function tryPlayNext()
{
    var play_next = document.querySelectorAll('a[aria-label="Play next video"]');
    if (play_next != null && play_next[0] != null)
        play_next[0].click();
}
function onMediaPlay()
{
    m_JavaScriptMediaCallbacks.onPlay();
}
function onMediaPause()
{
    m_JavaScriptMediaCallbacks.onPause();
}
function onMediaEnded()
{
    m_JavaScriptMediaCallbacks.onEnded();
    tryPlayNext();
}
function onMediaTimeUpdate()
{
    var v = document.getElementsByTagName("video")[0];
    if (v != null)
        m_JavaScriptMediaCallbacks.onTimeUpdate(v.currentTime);
}
function onMediaDurationChange()
{
    var v = document.getElementsByTagName("video")[0];
    if (v != null)
        m_JavaScriptMediaCallbacks.onDurationChange(v.getDuration());
}
