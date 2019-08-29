function getMedia()
{
    return document.getElementsByTagName("video")[0];
}
function getMediaCurrentTime()
{
    var media = getMedia();
    if (media != null)
        return media.currentTime;
    else
        return 0;
}
function getMediaDuration()
{
    var media = getMedia();
    if (media != null)
        return media.duration;
    else
        return 0;
}
function onMediaPlay()
{
    m_JavaScriptMediaCallbacks.onPlay();
    tryClickSkipAd();
}
function onMediaPause()
{
    m_JavaScriptMediaCallbacks.onPause();
    tryClickPlayNext();
}
function onMediaTimeUpdate()
{
    var time = getMediaCurrentTime();
    m_JavaScriptMediaCallbacks.onTimeUpdate(time);
    if (time < 10)
        tryClickSkipAd();
}
function onMediaDurationChange()
{
    m_JavaScriptMediaCallbacks.onDurationChange(getMediaDuration());
}
function onMediaLog(msg)
{
    m_JavaScriptMediaCallbacks.OnLog(msg);
}
function mediaSetEventListener()
{
    var media = getMedia();
    if (media != null)
    {
        media.removeEventListener('play', onMediaPlay);
        media.removeEventListener('pause', onMediaPause);
        media.removeEventListener('ended', onMediaPause);
        media.removeEventListener('timeupdate', onMediaTimeUpdate);
        media.removeEventListener('durationchange', onMediaDurationChange);
        media.addEventListener('play', onMediaPlay);
        media.addEventListener('pause', onMediaPause);
        media.addEventListener('ended', onMediaPause);
        media.addEventListener('timeupdate', onMediaTimeUpdate);
        media.addEventListener('durationchange', onMediaDurationChange);
    }
    if (media == null || media.paused || media.ended)
        onMediaPause();
    else
        onMediaPlay();
}
function mediaPlay()
{
    var media = getMedia();
    if (media != null) media.play();
}
function mediaPause()
{
    var media = getMedia();
    if (media != null) media.pause();
}
function mediaPlayPause()
{
    var media = getMedia();
    if (media != null)
    {
        if (media.paused)
            media.play();
        else
            media.pause();
    }
}
function mediaSkipToPrev()
{
    mediaPlayPause();
}
function mediaSkipToNext()
{
    var media = getMedia();
    if (media != null)
    {
        if (media.currentTime > 0 && media.currentTime < media.duration)
            media.currentTime = media.duration;
    }
}
function tryClickPlayNext()
{
    var media = getMedia();
    if (media != null && media.ended)
    {
        var playNext = document.querySelector('a[aria-label="Play next video"]');
        if (playNext != null)
            playNext.click();
    }
}
function tryClickSkipAd()
{
    var skipAd = document.querySelector(".ytp-ad-skip-button ");
    if (skipAd != null)
    {
        skipAd.click();
    }
}
