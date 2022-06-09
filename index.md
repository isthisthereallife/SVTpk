<h1>SVTpk - Privatkopiera från SVT Play</h1>

<hr>
<h2>Windows:</h2>
<h3><a href="https://mega.nz/file/J7IS3DLK#gNFpIkJsAmeLYkv06PfIBBsXhpLAJBe_gtKfgWkJSKs"> Ladda ner Version 0.2</a> <h7 className="x-small">(inklusive ffmpeg)</h7></h3>
<p><a href="https://mega.nz/file/h25E1C5a#5FKfcsGZl5_6ODMBJ_WxSE6SeJUrpxx9pLTUpxLwzvE">(Jag har redan ffmpeg på min dator)</a></p>
<h2>Mac</h2>
<p>Kommer snart!</p>
<h2>Linux</h2>
Inte aktuellt.
<hr>
<br/>
<ul>
    
    Uppdateringar
    <li>Klistrar automatiskt in kopierad adress om den är relevant.</li>

    0.2
    <li>Alla avsnitt går att ladda ner</li>
    <li>Möjligt att köa nedladdningar</li>

</ul>
<br/>
<hr>
<br/>
<br/>
<br/>
<a href="https://github.com/isthisthereallife/svtpk">Klicka här för att komma till källkoden.</a> 
<hr>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
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
