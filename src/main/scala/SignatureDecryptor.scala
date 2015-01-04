import java.util.logging.{Logger, Level}


sealed class CryptType

case object CryptHTML5 extends CryptType

case object CryptSWF extends CryptType

/**
 * Decrypts signature.
 * Both algorithms seems to be identical.
 *
 *
 * NOTE: algorithms changes pretty frequently, hence щту ырщгдв have player downloaded from time to time and decompiled.
 * JS have to be analyzed by hands or with custom grammar (for it's obfuscated).
 */
object SignatureDecryptor {
  val logger = Logger.getLogger(getClass.toString)
  val UncryptedType1 = "(&signature=[^&,]*)".r.unanchored
  val CriptedType1 = "sig=([^&,]*)".r.unanchored
  val CriptedType2 = "[&,]s=([^&,]*)".r.unanchored

  /**
   * s.ytimg.com/yts/jsbin/html5player-ru_RU-vfl91Z42B/html5player.js
   */
  def decryptHTML5(sig: String): String = {
    def reorder(str: Array[Char], b: Int) = {
      val c = str.charAt(0)
      str(0) = str(b % str.size)
      str(b) = c
      str
    }
    reorder(sig.toCharArray, 8).reverse.drop(1).mkString
  }

  /**
   * decompile https://s.ytimg.com/yts/swfbin/player-vfly1u_c5/watch_as3.swf
   *
   * Look into com.google.YouTube.util.SignatureDecipher.
   * @param sig to be converted into direct form.
   * @return decrypted signature
   */
  def decryptSWF(sig: String): String = {
    val result = sig.substring(9, 81).reverse +
      sig.charAt(0) +
      sig.substring(1, 8).reverse +
      sig.charAt(8)
    result
  }

  /**
   * have to be rewritten into Option result.
   * @param urlString to be decrypted.
   * @return formed signature part.
   */
  def apply(urlString: String) = {
    urlString match {
      case UncryptedType1(signature) => signature
      case CriptedType1(signature) => "&signature=" + SignatureDecryptor.decryptHTML5(signature)
      case CriptedType2(signature) => "&signature=" + SignatureDecryptor.decryptHTML5(signature)
      case _ => logger.log(Level.SEVERE, s"Can't find signature. Video is unavailable $urlString")
        ""
    }
  }
}