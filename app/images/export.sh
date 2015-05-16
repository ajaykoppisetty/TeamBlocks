#/bin/bash
function export {
	# echo exporting $1 dpi to $2
	inkscape --export-area-page --export-dpi $1 --export-png=$2 $3
	echo
}

if [ "$#" -ne 2 ]; then
	echo "Usage: $0 <svg file> <target file name>"
	exit 0
fi

outdir="../src/main/res/drawable"
dpi_regular=60
dpi_large=75
dpi_xlarge=112.5



export $dpi_regular $outdir-mdpi/$2 $1
export $(echo "scale=2; $dpi_regular*1.5" | bc) $outdir-hdpi/$2 $1
export $(echo "scale=2; $dpi_regular*2" | bc) $outdir-xhdpi/$2 $1
export $(echo "scale=2; $dpi_regular*3" | bc) $outdir-xxhdpi/$2 $1

echo

export $dpi_large $outdir-sw340-mdpi/$2 $1
export $(echo "scale=2; $dpi_large*1.5" | bc) $outdir-sw340-hdpi/$2 $1
export $(echo "scale=2; $dpi_large*2" | bc) $outdir-sw340-xhdpi/$2 $1
export $(echo "scale=2; $dpi_large*3" | bc) $outdir-sw340-xxhdpi/$2 $1

echo

export $dpi_xlarge $outdir-sw600-mdpi/$2 $1
export $(echo "scale=2; $dpi_xlarge*1.5" | bc) $outdir-sw600-hdpi/$2 $1
export $(echo "scale=2; $dpi_xlarge*2" | bc) $outdir-sw600-xhdpi/$2 $1
export $(echo "scale=2; $dpi_xlarge*3" | bc) $outdir-sw600-xxhdpi/$2 $1
