var v = document.getElementsByTagName("video")[0];
if (v != null)
{
    if (v.currentTime > 0 && v.currentTime < v.getDuration())
        v.currentTime = v.getDuration();
}
