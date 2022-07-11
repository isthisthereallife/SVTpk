<h1>SVTpk - Privatkopiera från SVT Play</h1>

<hr>
<h2>Windows:</h2>
<h3><a href="https://mega.nz/file/V74CFJgD#DEl7mHPQZFMARjYBmoYm9SwJfyazrT1zOLSZM_VoEsM"> Ladda ner Version 0.2.2</a> <h7 className="x-small">(inklusive ffmpeg)</h7></h3>
<p><a href="https://mega.nz/file/R3gRUawL#PqaYmtnvIzYuX-Rb7mMfBD69BlRuhkKSk5TueptwkqI">(Jag har redan ffmpeg på min dator)</a></p>
<h2>Mac</h2>
<p>Kommer snart!</p>
<h2>Linux</h2>
Inte aktuellt.
<hr>
<br/>
<ul style="list-style-type:square">
    <h4>Uppdateringshistorik</h4>
    <h5><a href="https://mega.nz/file/R3gRUawL#PqaYmtnvIzYuX-Rb7mMfBD69BlRuhkKSk5TueptwkqI">Version 0.2.2</a></h5>
    <li>Å, Ä, och Ö skrivs nu ut som de ska</li>
    <li>Lite mer förlåtande adressfält</li>
    <li>Bytt ikon på genvägen</li>
    <h5><a href="https://mega.nz/file/J7IS3DLK#gNFpIkJsAmeLYkv06PfIBBsXhpLAJBe_gtKfgWkJSKs">Version 0.2.1</a></h5>
    <li>Klistrar automatiskt in kopierad adress om den är relevant</li>
    <li>Små buggfixar</li>
    <h5><a href="https://mega.nz/file/kiwmRCwQ#56gIx8q8gr_oTTCFX3K_me3ApseWpFhR0RdAOvhAedg">Version 0.2</a></h5>
    <li>Alla avsnitt går att ladda ner</li>
    <li>Möjligt att köa nedladdningar</li>
    <h5><a href="https://mega.nz/file/h64yFLLY#AQNMfWHo9Sp9wTMAvbUa7OqNRPdyqU-9vK3D4uDYbVU">Version 0.1.1</a></h5>
    <li>Lagt till ikoner och bilder</li>
    <h5><a href="https://mega.nz/file/U7ABTIzZ#dtHnHtryLUInrxiRqcf0nT9QndljDshkQNde0o-A7pc">Version 0.1</a></h5>
    <li>Grundläggande funktionalitet</li>
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
