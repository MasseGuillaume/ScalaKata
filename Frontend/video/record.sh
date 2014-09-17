rm -f output.mp4 &&
play -n synth pl G2 pl B2 pl D3 pl G3 pl D4 pl G4 \
     delay 0 .05 .1 .15 .2 .25 remix - fade 0 4 .1 norm -1 && \
ffmpeg \
-s 1366x768 -f x11grab -i :0.0 \
-f alsa -i pulse \
-vcodec libx264 -b 3000k \
-acodec aac -strict experimental \
-af lowpass=f=5000 \
-s hd720 -ab 320k -r 25 -g 25 -threads 0 output.mp4 &&
mplayer output.mp4
