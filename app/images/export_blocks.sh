#/bin/bash

svg_file=blocks.svg
block_size=30
x_count=5
outdir="../src/main/res/drawable"
dpi=90
xlarge=1.5

function export {
	for x in `seq 0 $x_count`
	do
		x0=$((x*block_size))
		x1=$((x0+block_size))
		y0=$((0))
		y1=$((block_size))
		inkscape --export-area=$x0:$y0:$x1:$y1 --export-dpi $1 --export-png=$2/block_red_$x.png $svg_file

		y0=$((y0+block_size))
		y1=$((y1+block_size))
		inkscape --export-area=$x0:$y0:$x1:$y1 --export-dpi $1 --export-png=$2/block_yellow_$x.png $svg_file

		y0=$((y0+block_size))
		y1=$((y1+block_size))
		inkscape --export-area=$x0:$y0:$x1:$y1 --export-dpi $1 --export-png=$2/block_blue_$x.png $svg_file
	done
	echo
}


export $dpi $outdir-mdpi
export $(echo "scale=2; $dpi*1.5" | bc) $outdir-hdpi
export $(echo "scale=2; $dpi*2" | bc) $outdir-xhdpi
export $(echo "scale=2; $dpi*3" | bc) $outdir-xxhdpi

echo


export $dpi $outdir-xlarge-mdpi
export $(echo "scale=2; $dpi*1.5*$xlarge" | bc) $outdir-xlarge-hdpi
export $(echo "scale=2; $dpi*2*$xlarge" | bc) $outdir-xlarge-xhdpi
export $(echo "scale=2; $dpi*3*$xlarge" | bc) $outdir-xlarge-xxhdpi
