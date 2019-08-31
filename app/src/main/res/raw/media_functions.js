function getMedia()
{
    return document.getElementsByTagName("video")[0];
}

function hasMedia()
{
    return (getMedia() != null);
}

function isMediaPlaying()
{
    var media = getMedia();
    return (media != null && !isMediaPaused() && !isMediaEnded());
}

function isMediaPaused()
{
    var media = getMedia();
    return (media == null || media.paused);
}

function isMediaEnded()
{
    var media = getMedia();
    return (media == null || media.ended);
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

function mediaSeekTo(time)
{
    var media = getMedia();
    if (media != null)
        media.currentTime = time;
}

function onMediaPlay()
{
    m_JavaScriptMediaCallbacks.onMediaPlay();
}

function onMediaPause()
{
    m_JavaScriptMediaCallbacks.onMediaPause();
}

function onMediaTimeUpdate()
{
    m_JavaScriptMediaCallbacks.onMediaTimeUpdate(getMediaCurrentTime());
}

function onMediaDurationChange()
{
    m_JavaScriptMediaCallbacks.onMediaDurationChange(getMediaDuration());
}

function mediaLog(msg)
{
    m_JavaScriptMediaCallbacks.mediaLog("MediaLog : " + msg);
}

var mediaInterval = null;

function clearMediaInterval()
{
    if (mediaInterval != null)
    {
        clearInterval(mediaInterval);
        mediaInterval = null;
    }
}

function setMediaInterval()
{
    clearMediaInterval();
    mediaInterval = setInterval(onMediaInterval, 500);
}

function mediaSetEventListener()
{
    clearMediaInterval();
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
        setMediaInterval();
    }
    if (isMediaPlaying())
        onMediaPlay();
    else
        onMediaPause();
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
    if (hasMedia())
    {
        if (isMediaPaused())
            mediaPlay();
        else if (isMediaPlaying())
            mediaPause();
    }
}

function mediaSkipToPrev()
{
    mediaPlayPause();
}

function mediaSkipToNext()
{
    var time = getMediaCurrentTime();
    var duration = getMediaDuration();
    if (time > 0 && duration > 0 && time < duration)
        mediaSeekTo(duration);
}

function onMediaInterval()
{
    if (hasMedia())
    {
        if (isMediaEnded())
        {
            var playNext = document.querySelector('a[aria-label="Play next video"]');
            if (playNext != null)
                playNext.click();
        }

        if (isMediaPlaying() && (getMediaCurrentTime() < 10))
        {
            var skipAd = document.querySelector(".ytp-ad-skip-button ");
            if (skipAd != null)
                skipAd.click();
        }
    }
}
