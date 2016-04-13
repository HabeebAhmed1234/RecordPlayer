package com.cromiumapps.musicplayer.dialogfragment;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.fragments.BaseFragment;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.providers.MusicPlayerDatabase;
import com.cromiumapps.musicplayer.providers.SongsContentProvider;

/**
 * Created by habeebahmed on 3/20/16.
 */
public class AddToPlaylistDialogFragment extends DialogFragment implements TextView.OnEditorActionListener {
    private static final String EXTRA_SONG_ID = "extra_song_id";
    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }

    private int mSongId;
    private SongList mSongList;

    private EditText mNewPlaylistEditText;
    private View mSubmitNewPlaylistBtn;
    private RecyclerView mPlaylistsRecyclerView;


    public static AddToPlaylistDialogFragment newInstance(int songId) {
        AddToPlaylistDialogFragment fragment = new AddToPlaylistDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_SONG_ID, songId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public AddToPlaylistDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongList = ((BaseFragment.Host)getActivity()).getMusicFetcher().getAllSongsList();
        mSongId = getArguments().getInt(EXTRA_SONG_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_to_playlist, container);
        mNewPlaylistEditText = (EditText) view.findViewById(R.id.new_playlist_edit_text);
        mSubmitNewPlaylistBtn = view.findViewById(R.id.submit_new_playlist_btn);
        mPlaylistsRecyclerView = (RecyclerView) view.findViewById(R.id.playlists_recycerview);
        mPlaylistsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPlaylistsRecyclerView.setAdapter(new PlaylistsAdapter(getActivity()));
        // Show soft keyboard automatically
        mNewPlaylistEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mNewPlaylistEditText.setOnEditorActionListener(this);
        mSubmitNewPlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPlaylist();
            }
        });

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            addNewPlaylist();
            return true;
        }
        return false;
    }

    private void addNewPlaylist() {
        String newPlaylistName = mNewPlaylistEditText.getText().toString();
        if(validateNewPlaylistName(newPlaylistName)) {
            addSongToPlaylist(newPlaylistName);
        }
    }

    private boolean validateNewPlaylistName(String newPlaylistName) {
        for(int i = 0 ; i < mSongList.getPlayListsCount() ; i++) {
            if(mSongList.getPlaylistByIndex(i).name.toLowerCase().equals(newPlaylistName.toLowerCase())) {
                Toast.makeText(getActivity(), getString(R.string.error_playlist_already_exists), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void addSongToPlaylist(String playlist) {
        //TODO: add new playlist to database and refresh the songlist object
        Song song = mSongList.getSongById(mSongId);
        ContentValues mNewValues = new ContentValues();
        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_PLAYLIST, playlist);
        getActivity().getContentResolver().update(SongsContentProvider.TABLE_MUSIC_CONTENT_URI, mNewValues, MusicPlayerDatabase.SONGS_COL_SONG_ID + " = " + song.id, null);
        Toast.makeText(getActivity(), getString(R.string.added_to_playlist, playlist), Toast.LENGTH_SHORT).show();
        dismiss();
    }

    class PlaylistsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private LayoutInflater mInflater;

        PlaylistsAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PlaylistViewHolder(mInflater.inflate(R.layout.dialog_playlist_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final String playlistName = mSongList.getPlaylistByIndex(position).name;
            ((TextView)holder.itemView.findViewById(R.id.playlist_name)).setText(playlistName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addSongToPlaylist(playlistName);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSongList.getPlayListsCount();
        }

        class PlaylistViewHolder extends RecyclerView.ViewHolder{

            public PlaylistViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
