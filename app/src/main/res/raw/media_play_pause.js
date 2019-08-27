var v = document.getElementsByTagName("video")[0];
if (v != null)
{
    if (v.paused)
        v.play();
    else
        v.pause();
}
