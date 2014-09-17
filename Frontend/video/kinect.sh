sudo modprobe v4l2loopback

gst-launch -v v4l2src device=/dev/video1 ! \
  ffmpegcolorspace ! \
  video/x-raw-rgb ! \
  ffmpegcolorspace ! \
  video/x-raw-yuv,format=\(fourcc\)YUY2 ! \
  v4l2sink device=/dev/video2
