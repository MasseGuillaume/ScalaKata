import com.scalakata.eval._
@ScalaKata object TwitterMaze {
  html"<h1>Twitter's OSCON Coding Puzzle</h1>"
  import scala.util.Try
  val nl = System.lineSeparator

  def gliff(xs: String) = {
  	xs.stripMargin.split(nl).to[List]
  }

  val gliffs =
    Map(
      gliff("""|---
               |xxx
               |---""") -> '━',

      gliff("""|-x-
               |-x-
               |-x-""") -> '┃',

      gliff("""|---
               |-xx
               |-x-""") -> '┏',

      gliff("""|---
               |xx-
               |-x-""") -> '┓',

      gliff("""|-x-
               |-xx
               |---""") -> '┗',

      gliff("""|-x-
               |xx-
               |---""") -> '┛',

      gliff("""|-x-
               |-xx
               |-x-""") -> '┣',

      gliff("""|-x-
               |xx-
               |-x-""") -> '┫',

      gliff("""|---
               |xxx
               |-x-""") -> '┳',

      gliff("""|-x-
               |xxx
               |---""") -> '┻',

      gliff("""|-x-
               |xxx
               |-x-""") -> '╋',

      gliff("""|---
               |x--
               |---""") -> '╸',


      gliff("""|-x-
               |---
               |---""") -> '╹',

      gliff("""|---
               |--x
               |---""") -> '╺',

      gliff("""|---
               |---
               |-x-""") -> '╻'
    )


  def slice2d(fromX: Int, toX: Int, fromY: Int, toY: Int)(a: List[String]) = {
      a.slice(fromY, toY + 1).map(_.substring(fromX, toX + 1))
  }

  val omaze =
        """|-----------------------
           |-xxxxxxxxxxxxxxxxxxxxx-
           |-x---x-----x-----x-x-x-
           |-x-x-x-xxx-xxxxx-x-x-x-
           |-x-x-x---x-----x-x---x-
           |-x-xxxxxxx-x-xxx-x-x-x-
           |-x---------x-----x-x-x-
           |-x-xxx-xxxxx-xxxxx-x-x-
           |-x-x---x---x-x-----x-x-
           |-x-xxxxx-x-x-x-xxx-x---
           |-------x-x-x-x-x-x-xxx-
           |-xxxxx-x-x-x-x-x-x-x-x-
           |-x-----x-x-x---x-x-x-x-
           |-x-xxxxxxx-xxxxx-x-x-x-
           |-x-------x-------x---x-
           |-xxxxxxxxxxxxxxxxxxxxx-
           |-----------------------""".stripMargin

  val maze = gliff(omaze)

  def draw(maze: List[List[Char]]) = maze.map(_.mkString("")).mkString(nl)

  val empty = ' '
  val holes =
    (0 until maze.length).sliding(3, 1).to[List].map { case ys =>
        (0 until maze(0).length).sliding(3, 1).to[List].map { case xs =>
            val slice = slice2d(xs.head, xs.last, ys.head, ys.last)(maze)
            gliffs.getOrElse(slice, empty)
        }
    }

  val east = Set('╋', '┓', '━', '┫', '┳', '╸', '┛', '┻')
  val west = Set('╋', '━', '┏', '┣', '┗', '┳', '╺', '┻')
  val north = Set('╋', '┓', '┏', '┃', '┣', '┫', '┳', '╻')
  val south = Set('╋', '╹', '┃', '┣', '┗', '┫', '┛', '┻')

  val sol =
    holes.zipWithIndex.map{ case (xs, i) =>{
        xs.zipWithIndex.map{ case (c, j) =>
            def get(x: Int, y: Int): Char = {
                Try(holes(x)(y)).getOrElse(empty)
            }
            if(c != empty) c
            else if(north.contains(get(i-1, j)) ||
                    south.contains(get(i+1, j))) '┃'
            else if(east.contains(get(i, j+1)) ||
                    west.contains(get(i, j-1))) '━'
            else if(north.contains(get(i-2, j))) '╹'
            else if(south.contains(get(i+2, j))) '╻'
            else if(east.contains(get(i, j+2))) '╺'
            else if(west.contains(get(i, j-2))) '╸'
            else ' '
        }
    }}


  draw(holes)
  omaze.replaceAll("-", " ")
  draw(sol)
}
