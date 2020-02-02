package com.github.slashmax.aabrowser;

class BookmarkItem
{
    private static final String TAG = "BookmarkItem";

    private String m_Name;
    private String m_Url;

    BookmarkItem(String name, String url)
    {
        m_Name = name;
        m_Url = url;
    }

    void SetName(String name)
    {
        m_Name = name;
    }

    void SetUrl(String url)
    {
        m_Url = url;
    }

    String GetName()
    {
        return m_Name;
    }

    String GetUrl()
    {
        return m_Url;
    }
}
