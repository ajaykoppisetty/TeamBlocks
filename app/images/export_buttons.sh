#/bin/bash

svg_file=buttons.svg
outdir="../src/main/res/drawable"

dpi_regluar=60
dpi_large=75
dpi_xlarge=112.5

function export {
	# regular buttons
	# inkscape --export-area=0:0:76,5:46.5 --export-dpi $1 --export-png=$2/button_down_single.png $svg_file
	# inkscape --export-area=76,5:0:153:46,5 --export-dpi $1 --export-png=$2/button_down_all.png $svg_file
	inkscape --export-area=153:0:229,5:46,5 --export-dpi $1 --export-png=$2/button_done.png $svg_file
	# inkscape --export-area=0:46,5:76,5:93 --export-dpi $1 --export-png=$2/button_left.png $svg_file
	# inkscape --export-area=76,5:46,5:153:93 --export-dpi $1 --export-png=$2/button_rotate.png $svg_file
	# inkscape --export-area=153:46,5:229,5:93 --export-dpi $1 --export-png=$2/button_right.png $svg_file

	# selected buttons
	# inkscape --export-area=0:93:76,5:139.5 --export-dpi $1 --export-png=$2/button_down_single_selected.png $svg_file
	# inkscape --export-area=76,5:93:153:139,5 --export-dpi $1 --export-png=$2/button_down_all_selected.png $svg_file
	inkscape --export-area=153:93:229,5:139,5 --export-dpi $1 --export-png=$2/button_done_selected.png $svg_file
	# inkscape --export-area=0:139,5:76,5:186 --export-dpi $1 --export-png=$2/button_left_selected.png $svg_file
	# inkscape --export-area=76,5:139,5:153:186 --export-dpi $1 --export-png=$2/button_rotate_selected.png $svg_file
	# inkscape --export-area=153:139,5:229,5:186 --export-dpi $1 --export-png=$2/button_right_selected.png $svg_file
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
