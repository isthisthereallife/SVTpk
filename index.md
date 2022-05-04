<h1>SVTpk - Privatkopiera från SVT Play</h1>

<h3><a href="https://mega.nz/folder/ZjYzlTZD#fqac1VEosxcg1iC8D64pyQ">Klicka här för att komma till nedladdningssidan.<a/></h3>
<hr>
<a href="https://github.com/isthisthereallife/svtpk">Klicka här för att komma till källkoden.</a> 
<hr>
För att köra en egen build av programmet krävs att <a href="https://www.ffmpeg.org/download.html">FFmpeg</a> finns som miljövariabel. <a href="https://windowsloop.com/install-ffmpeg-windows-10/">Det gör du såhär.</a> 


För att köra programmet i din IDE krävs att du har en <a href="https://gluonhq.com/products/javafx/">JavaFX SDK</a> på din dator, samt att din VM har adress till modulen javafx.controls.

<h6>Lägg till detta som VM Options under Run/Debug Configurations (byt ut %Mappnamn% till relevant adress)</h6>

    --module-path "%Mappnamn%\javafx-sdk-17.0.2\lib" --add-modules=javafx.controls

    exempelvis
    --module-path "C:\Program Files\Java\javafx-sdk-17.0.2\lib" --add-modules=javafx.controls
