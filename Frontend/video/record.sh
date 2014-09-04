rm output.mp4 &&
ffmpeg \
-s 1366x768 -f x11grab -i :0.0 \
-f alsa -i pulse \
-vcodec libx264 -b 5000k \
-acodec aac -strict experimental \
-af highpass=f=20,lowpass=f=3000 \
-s hd720 -ab 320k -r 25 -g 25 -threads 0 output.mp4 &&
mplayer output.mp4
