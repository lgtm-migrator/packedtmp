package app.packed.extension;

import app.packed.component.instance.ExtTestCommon;
import app.packed.extension.sandbox.Extensor;

// Vil sige den er god til at dokumentere hvem der er hvem. Men vi behoever jo egentlig ikke en faelles klasse

// ExtensionWirelet -> Har vi brug for at vide hvilken extension vi skal brokke os over ikke eksistere
// Extensor -> Har vi brug for at vide hvilke extensors kan finde hinanden
// ExtensionMirror -> Har vi brug for at vide hvilken extension vi skal spoerge om mirroret...

@SuppressWarnings("rawtypes") // Eclipse being a ...
public sealed interface ExtensionMember<E extends Extension> permits Extension,ExtensionBean,ExtensionMirror,ExtensionWirelet,Extensor,ExtTestCommon {}

// ExtensionMirror vil vi helst have @MemberOf...
// Mest fordi vi ikke gider at brugerne skal skrive ExtensionMirror<?>... 