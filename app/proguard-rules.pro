# kotlinx.serialization gera serializadores estáticos por classe @Serializable.
-keepclassmembers class com.simplesfinancas.app.** {
	*** Companion;
}
-keepclasseswithmembers class com.simplesfinancas.app.** {
	kotlinx.serialization.KSerializer serializer(...);
}
