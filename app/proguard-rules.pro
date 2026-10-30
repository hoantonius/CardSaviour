# kotlinx.serialization and type-safe Navigation look up generated serializers.
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod
-keep,includedescriptorclasses class id.cardframe.app.ui.nav.**$$serializer { *; }
-keepclassmembers class id.cardframe.app.ui.nav.** {
    *** Companion;
}
-dontwarn com.google.mlkit.**
-dontwarn com.google.zxing.**
