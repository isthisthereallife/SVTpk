<h1>SVTpk - Privatkopiera från SVT Play</h1>

<h2><a href="https://mega.nz/folder/ZjYzlTZD#fqac1VEosxcg1iC8D64pyQ">Klicka här för att komma till nedladdningssidan.</a></h2>
<hr>
<a href="https://github.com/isthisthereallife/svtpk">Klicka här för att komma till källkoden.</a> 
<hr>
<br/>
<br/>
<h3>För utvecklare</h3>
<h4>För att köra en egen build av programmet krävs att <a href="https://www.ffmpeg.org/download.html">FFmpeg</a> finns som miljövariabel. <a href="https://windowsloop.com/install-ffmpeg-windows-10/">Det gör du såhär.</a> </h4>
<br/>
<h4>För att köra programmet i din IDE krävs att du har en <a href="https://gluonhq.com/products/javafx/">JavaFX SDK</a> på din dator, samt att din VM har adress till modulen javafx.controls.</h4>
<br/>
<h4>Lägg till detta som VM Options under Run/Debug Configurations:</h4>

    --module-path "%Mappnamn%\javafx-sdk-17.0.2\lib" --add-modules=javafx.controls

<br/>
<br/>
<p>byt ut %Mappnamn% till relevant adress, exempelvis:</p>
<br/>

    --module-path "C:\Program Files\Java\javafx-sdk-17.0.2\lib" --add-modules=javafx.controls
