package com.indieweb.indigenous.microsub.channel;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.microsub.manage.ManageChannelActivity;
import com.indieweb.indigenous.microsub.timeline.TimelineActivity;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private boolean showRefreshMessage = false;
    private ListView listChannel;
    private SwipeRefreshLayout refreshLayout;
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    private User user;
    private String debugResponse;
    private String readLater;
    private LinearLayout noConnection;
    private RelativeLayout layout;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_channels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.actionButton).setOnClickListener(this);
        listChannel = view.findViewById(R.id.channel_list);
        refreshLayout = view.findViewById(R.id.refreshChannels);
        noConnection = view.findViewById(R.id.noConnection);
        layout = view.findViewById(R.id.channel_root);

        user = new Accounts(getContext()).getCurrentUser();
        requireActivity().setTitle(getString(R.string.nav_reader));

        if (user.getMicrosubEndpoint().length() > 0) {
            setHasOptionsMenu(true);
            refreshLayout.setOnRefreshListener(this);
            refreshLayout.setRefreshing(true);
            readLater = Preferences.getPreference(requireContext(), "pref_key_read_later", "");
            refreshLayout.setVisibility(View.VISIBLE);
            listChannel.setVisibility(View.VISIBLE);
            startChannels();
        }
        else {
            refreshLayout.setVisibility(View.GONE);
            listChannel.setVisibility(View.GONE);
            TextView noMicrosubEndpoint = view.findViewById(R.id.noMicrosubEndpoint);
            noMicrosubEndpoint.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start channels.
     */
    private void startChannels() {
        noConnection.setVisibility(View.GONE);
        Channels = new ArrayList<>();
        listChannel.setVisibility(View.VISIBLE);
        adapter = new ChannelListAdapter(requireContext(), Channels, readLater);
        listChannel.setAdapter(adapter);
        loadChannels();
    }

    @Override
    public void onRefresh() {
        showRefreshMessage = true;
        startChannels();
    }

    /**
     * Get channels.
     */
    private void loadChannels() {

        if (!new Connection(requireContext()).hasConnection()) {
            showRefreshMessage = false;
            checkRefreshingStatus();
            noConnection.setVisibility(View.VISIBLE);
            return;
        }

        String microsubEndpoint = user.getMicrosubEndpoint();
        if (microsubEndpoint.contains("?")) {
            microsubEndpoint += "&action=channels";
        }
        else {
            microsubEndpoint += "?action=channels";
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, microsubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object;
                            debugResponse = response;
                            JSONObject microsubResponse = new JSONObject(response);
                            JSONArray channelList = microsubResponse.getJSONArray("channels");

                            int index = 0;
                            int unreadChannels = 0;
                            int totalUnread = 0;
                            for (int i = 0; i < channelList.length(); i++) {
                                object = channelList.getJSONObject(i);
                                Channel channel = new Channel();
                                channel.setUid(object.getString("uid"));
                                channel.setName(object.getString("name"));

                                Integer unread = 0;
                                if (object.has("unread")) {
                                    Object unreadCheck = object.get("unread");
                                    if (unreadCheck instanceof Integer) {
                                        unread = (Integer) unreadCheck;
                                        totalUnread += unread;
                                        if (unread > 0) {
                                            unreadChannels++;
                                        }
                                    }
                                    if (unreadCheck instanceof Boolean) {
                                        if ((Boolean) unreadCheck) {
                                            unread = -1;
                                        }
                                    }
                                }

                                channel.setUnread(unread);
                                Channels.add(index++, channel);
                            }

                            try {
                                if (Preferences.getPreference(getContext(), "pref_key_unread_items_channel", false) && unreadChannels > 1 && totalUnread > 0) {
                                    Channel channel = new Channel();
                                    channel.setUid("global");
                                    channel.setName("Unread items");
                                    channel.setUnread(totalUnread);
                                    Channels.add(0, channel);
                                }
                            }
                            catch (Exception ignored) {}

                            adapter.notifyDataSetChanged();
                            checkRefreshingStatus();
                            Indigenous app = Indigenous.getInstance();
                            app.setChannels(Channels);
                        }
                        catch (JSONException e) {
                            showRefreshMessage = false;
                            Snackbar.make(layout, String.format(getString(R.string.channel_list_parse_error), e.getMessage()), Snackbar.LENGTH_SHORT).show();
                            checkRefreshingStatus();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Context context = getContext();
                        Utility.parseNetworkError(error, context, R.string.channel_network_fail, R.string.channel_fail);
                        showRefreshMessage = false;
                        checkRefreshingStatus();
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(getRequest);
    }

    /**
     * Checks the state of the pull to refresh.
     */
    private void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Snackbar.make(layout, getString(R.string.channels_refreshed), Snackbar.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.actionButton) {
            ((MainActivity) requireActivity()).openDrawer(R.id.nav_create);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_menu, menu);

        boolean debugJson = Preferences.getPreference(getActivity(), "pref_key_debug_microsub_channels", false);
        if (debugJson) {
            MenuItem item = menu.findItem(R.id.channels_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        boolean search = Preferences.getPreference(getActivity(), "pref_key_search_global", false);
        if (search) {
            MenuItem item = menu.findItem(R.id.channel_search);
            if (item != null) {
                item.setVisible(true);

                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                searchView = (SearchView) item.getActionView();

                if (searchView != null) {
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

                    queryTextListener = new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return true;
                        }
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (query.length() > 0) {
                                Intent timelineActivity = new Intent(getContext(), TimelineActivity.class);
                                timelineActivity.putExtra("channelId", "global");
                                timelineActivity.putExtra("search", query);
                                startActivity(timelineActivity);
                            }
                            return true;
                        }
                    };
                    searchView.setOnQueryTextListener(queryTextListener);
                }

            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.channels_manage:
                Intent manageChannels = new Intent(getContext(), ManageChannelActivity.class);
                startActivity(manageChannels);
                return true;

            case R.id.channel_list_refresh:
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startChannels();
                return true;

            case R.id.channels_debug:
                Intent i = new Intent(getContext(), DebugActivity.class);
                Indigenous app = Indigenous.getInstance();
                app.setDebug(debugResponse);
                startActivity(i);
                return true;
        }

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onOptionsItemSelected(item);
    }

}
