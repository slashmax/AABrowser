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
    return (media != null && !isMediaPaused() && !isMediaStopped());
}

function isMediaPaused()
{
    var media = getMedia();
    return (media == null || media.paused);
}

function isMediaStopped()
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
    m_JavaScriptMediaCallbacks.onMediaPlay(getMediaCurrentTime());
}

function onMediaPause()
{
    m_JavaScriptMediaCallbacks.onMediaPause(getMediaCurrentTime());
}

function onMediaStop()
{
    m_JavaScriptMediaCallbacks.onMediaStop(getMediaCurrentTime());
}

function onMediaTimeUpdate()
{
    m_JavaScriptMediaCallbacks.onMediaTimeUpdate(getMediaCurrentTime());
}

function onMediaDurationChange()
{
    m_JavaScriptMediaCallbacks.onMediaDurationChange(getMediaDuration());
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
        media.removeEventListener('ended', onMediaStop);
        media.removeEventListener('timeupdate', onMediaTimeUpdate);
        media.removeEventListener('durationchange', onMediaDurationChange);

        media.addEventListener('play', onMediaPlay);
        media.addEventListener('pause', onMediaPause);
        media.addEventListener('ended', onMediaStop);
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

function mediaSeekToStart()
{
    mediaSeekTo(0);
}

function mediaSeekToEnd()
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
        if (isMediaPaused())
        {
            var playNextDiv = document.querySelector(".ytp-upnext");
            if (playNextDiv != null && playNextDiv.style.display != "none")
            {
                var playNext = document.querySelector(".ytp-upnext-autoplay-icon");
                if (playNext != null)
                {
                    playNext.click();
                }
            }
        }

        if (isMediaPlaying())
        {
            var skipAd = document.querySelector(".ytp-ad-skip-button");
            if (skipAd != null)
            {
                mediaSeekToEnd();
            }

            var closeAd = document.querySelectorAll(".ytp-ad-overlay-close-button");
            if (closeAd != null && closeAd.length > 0)
            {
                for (i = 0; i < closeAd.length; i++)
                {
                    closeAd[i].click();
                }
            }
        }
    }
}
