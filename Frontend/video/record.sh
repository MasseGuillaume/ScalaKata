rm -f output.mp4 &&
sleep 5 &&
ffmpeg \
-s 1366x768 -f x11grab -i :0.0 \
-f alsa -i pulse \
-vcodec libx264 -b 3000k \
-acodec aac -strict experimental \
-af lowpass=f=5000 \
-s hd720 -ab 320k -r 25 -g 25 -threads 0 output.mp4 &&
mplayer output.mp4
