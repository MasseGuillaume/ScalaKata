import com.scalakata.eval._

object Menu {
  def apply = {
    html"""
    <style>
    .wtf{
      position: fixed;
        top: 0;
        left: 0;
        margin: 50px;
    }
    </style>
    <div class="wtf">
      <a href="/toto/titi">Toto</a>
    </div>
    """
  }
}
