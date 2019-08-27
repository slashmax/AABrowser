var v = document.getElementsByTagName("video")[0];
if (v != null)
{
    v.removeEventListener('play', onMediaPlay);
    v.removeEventListener('pause', onMediaPause);
    v.removeEventListener('ended', onMediaEnded);
    v.removeEventListener('timeupdate', onMediaTimeUpdate);
    v.removeEventListener('durationchange', onMediaDurationChange);
    v.addEventListener('play', onMediaPlay);
    v.addEventListener('pause', onMediaPause);
    v.addEventListener('ended', onMediaEnded);
    v.addEventListener('timeupdate', onMediaTimeUpdate);
    v.addEventListener('durationchange', onMediaDurationChange);
}
if (v == null || v.paused)
{
    onMediaPause();
}
else
{
    onMediaPlay();
}
