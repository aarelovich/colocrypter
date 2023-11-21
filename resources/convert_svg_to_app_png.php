<?php

$SIZES = [
   "mipmap-hdpi"      => 72,
   "mipmap-mdpi"      => 48,   
   "mipmap-xhdpi"     => 96,  
   "mipmap-xxhdpi"    => 144,   
   "mipmap-xxxhdpi"   => 192       
];

$input = ["app_icon_round.svg", "app_icon.svg"];

foreach ($input as $svg_file){

   if (str_contains($svg_file,"round")) $filename = "ic_launcher_round.png";
   else $filename = "ic_launcher.png";

   foreach ($SIZES as $dirname => $size){

      echo "Creating image size $size of type $filename\n";

      if (!is_dir($dirname)){
         mkdir($dirname);
      }      
      $output = $dirname . "/$filename";
      $cmd = "inkscape -w $size -h $size $svg_file -o $output";

      shell_exec($cmd);

   }

}

echo "Done\n";


?>
