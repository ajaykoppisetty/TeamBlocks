#/bin/bash

svg_file=blocks.svg
block_size=30
x_count=7
outdir="../src/main/res/drawable"

dpi_regluar=60
dpi_large=75
dpi_xlarge=112.5

function export {
	for x in `seq 6 $x_count`
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


export $dpi_regluar $outdir-mdpi
export $(echo "scale=2; $dpi_regluar*1.5" | bc) $outdir-hdpi
export $(echo "scale=2; $dpi_regluar*2" | bc) $outdir-xhdpi
export $(echo "scale=2; $dpi_regluar*3" | bc) $outdir-xxhdpi

echo

export $dpi_large $outdir-sw340dp-mdpi
export $(echo "scale=2; $dpi_large*1.5" | bc) $outdir-sw340dp-hdpi
export $(echo "scale=2; $dpi_large*2" | bc) $outdir-sw340dp-xhdpi
export $(echo "scale=2; $dpi_large*3" | bc) $outdir-sw340dp-xxhdpi

echo

export $dpi_xlarge $outdir-sw600dp-mdpi
export $(echo "scale=2; $dpi_xlarge*1.5" | bc) $outdir-sw600dp-hdpi
export $(echo "scale=2; $dpi_xlarge*2" | bc) $outdir-sw600dp-xhdpi
export $(echo "scale=2; $dpi_xlarge*3" | bc) $outdir-sw600dp-xxhdpi
