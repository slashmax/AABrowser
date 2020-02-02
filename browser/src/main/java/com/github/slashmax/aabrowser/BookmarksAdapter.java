package com.github.slashmax.aabrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder>
{
    private static final String TAG = "BookmarksAdapter";
    private static final String PREFERENCE_NAME = "BOOKMARKS";

    interface OnBookmarkListener
    {
        void onBookmark(String url);
    }

    private Context                 m_Context;
    private OnBookmarkListener      m_Listener;
    private ArrayList<BookmarkItem> m_BookmarkItems;


    BookmarksAdapter()
    {
        m_BookmarkItems = new ArrayList<>();
    }

    void setContext(Context context)
    {
        m_Context = context;
    }

    void setListener(OnBookmarkListener listener)
    {
        m_Listener = listener;
    }

    void onCreate()
    {
        LoadBookmarks();
    }

    void onDestroy()
    {
        SaveBookmarks();

        m_Context = null;
        m_Listener = null;
        m_BookmarkItems = null;
    }

    void LoadBookmarks()
    {
        SharedPreferences sharedPref = m_Context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        int count = sharedPref.getInt("BookmarksCount", 0);
        m_BookmarkItems.clear();
        for (int i = 0; i < count; i++)
        {
            String name = sharedPref.getString("BookmarkName" + i, "");
            String url = sharedPref.getString("BookmarkUrl" + i, "");
            m_BookmarkItems.add(new BookmarkItem(name, url));
        }
        notifyDataSetChanged();
    }

    void SaveBookmarks()
    {
        SharedPreferences sharedPref = m_Context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.putInt("BookmarksCount", m_BookmarkItems.size());
        for (int i = 0; i < m_BookmarkItems.size(); i++)
        {
            editor.putString("BookmarkName" + i, m_BookmarkItems.get(i).GetName());
            editor.putString("BookmarkUrl" + i, m_BookmarkItems.get(i).GetUrl());
        }
        editor.apply();
    }

    void Add(String name, String url)
    {
        int position = GetUrlPos(url);
        if (position == -1)
        {
            position = m_BookmarkItems.size();
            m_BookmarkItems.add(new BookmarkItem(name, url));
            notifyItemInserted(position);
        }
        else
        {
            m_BookmarkItems.get(position).SetName(name);
            notifyItemChanged(position);
        }
    }

    void Remove(int position)
    {
        if (position >= 0 && position < m_BookmarkItems.size())
        {
            m_BookmarkItems.remove(position);
            notifyItemRemoved(position);
        }
    }
    void Remove(String url)
    {
        Remove(GetUrlPos(url));
    }

    void Move(int from, int to)
    {
        m_BookmarkItems.add(to, m_BookmarkItems.remove(from));
        notifyItemMoved(from, to);
    }
    
    int GetUrlPos(String url)
    {
        for (int i = 0; i < m_BookmarkItems.size(); i++)
            if (m_BookmarkItems.get(i).GetUrl().equalsIgnoreCase(url))
                return i;
        return -1;
    }

    class BookmarkViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView      m_Name;
        private final TextView      m_Url;

        BookmarkViewHolder(@NonNull View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (m_Listener != null)
                    {
                        int position = getAdapterPosition();
                        if (position >=0 && position < m_BookmarkItems.size())
                            m_Listener.onBookmark(m_BookmarkItems.get(position).GetUrl());
                    }
                }
            });
            m_Name = itemView.findViewById(R.id.m_BookmarkName);
            m_Url = itemView.findViewById(R.id.m_BookmarkUrl);
            ImageButton delete = itemView.findViewById(R.id.m_BookmarkDeleteButton);
            delete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Remove(getAdapterPosition());
                }
            });
        }

        void setName(String name)
        {
            if (m_Name != null)
                m_Name.setText(name);
        }

        void setUrl(String url)
        {
            if (m_Url != null)
                m_Url.setText(url);
        }
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position)
    {
        if (m_BookmarkItems != null && position < m_BookmarkItems.size())
        {
            holder.setName(m_BookmarkItems.get(position).GetName());
            holder.setUrl(m_BookmarkItems.get(position).GetUrl());
        }
    }

    @Override
    public int getItemCount()
    {
        if (m_BookmarkItems == null)
            return 0;
        return m_BookmarkItems.size();
    }
}
