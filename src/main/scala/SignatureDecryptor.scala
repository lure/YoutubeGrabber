import java.util.logging.{Logger, Level}


/**
 * Decrypts signature.
 *
 * NOTE: algorithms changes pretty frequently, hence the save way is to have player downloaded from time to time and decompiled.
 * JS have to be analyzed by hands or by custom grammar (for it's obfuscated).
 */
object SignatureDecryptor {
  val logger = Logger.getLogger(getClass.toString)

  /**
   * read http://s.ytimg.com/yts/jsbin/html5player-ru_RU-vfl91Z42B/html5player.js
   */
  def decryptHTML5(sig: String): String = {
    def reorder(str: Array[Char], b: Int) = {
      val c = str.charAt(0)
      str(0) = str(b % str.size)
      str(b) = c
      str
    }
    val str = sig.toCharArray
    reorder(str, 8).reverse.drop(1).mkString
  }

  /**
   * decompile https://s.ytimg.com/yts/swfbin/player-vfly1u_c5/watch_as3.swf
   *
   * Look into YouTube.util.SignatureDecipher.
   * @param sig to be converted into direct form.
   * @return decrypted signature
   */
  def decryptSWF(sig: String): String = {
    sig.substring(9, 81).reverse +
      sig.charAt(0) +
      sig.substring(1, 8).reverse +
      sig.charAt(8)
  }

  /**
   * have to be rewritten into Option result.
   * @param urlString to be decrypted.
   * @return formed signature part.
   */
  def apply(urlString: String) = {
    //http://www.jwz.org/hacks/youtubedown
    val UncryptedSig1 = "(&signature=[^&,]*)".r.unanchored
    val UncryptedSig2 = "sig=([^&,]*)".r.unanchored
    val CriptedSig3 = "[&,]s=([^&,]*)".r.unanchored

    urlString match {
      case UncryptedSig1(signature) => signature
      case UncryptedSig2(signature) => "&signature=" + signature
      case CriptedSig3(signature) => "&signature=" + SignatureDecryptor.decryptSWF(signature)
      case _ => logger.log(Level.SEVERE, s"Can't find signature. Video is unavailable $urlString")
        ""
    }
  }
}

