import java.util.Stack

class WaveFunctionCollapse {
	private val grid = Grid()
	
	fun collapse() {
		while (!this.grid.isCollapsed()) {
			if (!this.iterate()) break
		}
		this.grid.print(markMistakes = true)
	}

	private fun iterate(): Boolean {
		val minEntropySquare = this.getMinEntropySquare() ?: return false
		this.collapseAt(minEntropySquare)
		if (!this.grid.isCollapsed()) this.propagateChangesFrom(minEntropySquare)
		return true
	}

	private fun getMinEntropySquare(): Point? {
		val entropies = Array(Grid.SIZE) { y ->
			IntArray(Grid.SIZE) { x ->
				this.getPossibleStatesOf(Point(x, y)).size
			}
		}

		var minEntropySquares = mutableListOf<Point>()
		var minEntropy = Int.MAX_VALUE

		entropies.forEachIndexed { y, row ->
			row.forEachIndexed { x, entropy ->
				if (entropy != 0) {
					if (entropy == minEntropy) {
						minEntropySquares.add(Point(x, y))
					} else if (entropy < minEntropy) {
						minEntropy = entropy
						minEntropySquares = mutableListOf(Point(x, y))
					}
				}
			}
		}

		return if (minEntropySquares.isEmpty()) {
			return null
		} else minEntropySquares.random()
	}

	private fun getPossibleStatesOf(point: Point): List<Int> {
		if (this.grid.get(point) != Grid.NOT_COLLAPSED) return emptyList()

		val possibleStates = mutableListOf<Int>()

		Grid.STATES.forEach {
			if (!(this.grid.doesColumnContain(point.x, it)
						or this.grid.doesRowContain(point.y, it)
						or this.grid.doesBoxContain(point, it))
			) {
				possibleStates.add(it)
			}
		}

		return possibleStates
	}

	private fun collapseAt(point: Point) {
		val possibleStates = this.getPossibleStatesOf(point)
		this.grid.set(point, possibleStates.random())
	}

	private fun collapseAt(point: Point, collapseTo: Int) {
		this.grid.set(point, collapseTo)
	}

	private fun propagateChangesFrom(point: Point) {
		val toVisit = Stack<Point>()
		toVisit.push(point)

		// While there are squares to propagate
		while (toVisit.isNotEmpty()) {
			val currentPoint = toVisit.pop()

			// Go over the squares affected by the current point
			for (affectedPoint in this.grid.affectedSquaresOf(currentPoint)) {
				// If the affected point had already been collapsed, continue to the next point
				if (this.grid.get(affectedPoint) != Grid.NOT_COLLAPSED) continue

				val possibleStates = this.getPossibleStatesOf(affectedPoint)

				// If there is only one possible state for this affected square, collapse it to that state
				// and add it to the stack of squares to propagate changes from.
				if (possibleStates.size == 1) {
					collapseAt(affectedPoint, possibleStates.first())
					toVisit.push(affectedPoint)
				}
			}
		}
	}

	data class Point(
		val x: Int = 0,
		val y: Int = 0
	)

	class Grid {
		private val grid = Array(SIZE) { IntArray(SIZE) { NOT_COLLAPSED } }

		fun get(x: Int, y: Int): Int =
			this.grid[y][x]

		fun get(point: Point): Int =
			this.grid[point.y][point.x]

		fun set(point: Point, value: Int) {
			this.grid[point.y][point.x] = value
		}

		fun print(markMistakes: Boolean = false) {
			for (y in 0 until 9) {
				print("| ")
				for (x in 0 until 9) {
					print(
						"${
							if (grid[y][x] == 0) {
								if (markMistakes) "!" else 0
							} else {
								grid[y][x]
							}
						} | "
					)
				}
				println()
			}
			if (markMistakes) {
				val mistakes = this.grid.sumOf { row -> row.count { it == NOT_COLLAPSED } }
				println("Finished with $mistakes mistakes.")
			}
		}

		fun isCollapsed(): Boolean =
			this.grid.none { row ->
				NOT_COLLAPSED in row
			}

		fun doesRowContain(y: Int, value: Int): Boolean = value in this.grid[y]

		fun doesColumnContain(z: Int, value: Int): Boolean =
			(0 until SIZE).any { y ->
				this.get(z, y) == value
			}

		fun doesBoxContain(point: Point, value: Int): Boolean {
			val (boxColumn, boxRow) = this.rowAndColumnIndexOf(point)
			return (0..2).any { y ->
				(0..2).any { x ->
					this.get(boxColumn + x, boxRow + y) == value
				}
			}
		}

		private fun rowAndColumnIndexOf(point: Point): Pair<Int, Int> {
			// Convert the row and column to 0..2 range with integer division,
			// and then to 0|3|6
			return (point.x / 3 * 3) to (point.y / 3 * 3)
		}

		fun affectedSquaresOf(point: Point): Set<Point> =
			(this.columnOf(point) + this.rowOf(point) + this.squareOf(point)) - point

		private fun columnOf(point: Point): Set<Point> =
			List(SIZE) { y ->
				Point(point.x, y)
			}.toSet()

		private fun rowOf(point: Point): Set<Point> =
			List(SIZE) { x ->
				Point(x, point.y)
			}.toSet()

		private fun squareOf(point: Point): Set<Point> {
			val (boxColumn, boxRow) = this.rowAndColumnIndexOf(point)

			return (0..2).flatMap { y ->
				(0..2).map { x ->
					Point(boxColumn + x, boxRow + y)
				}
			}.toSet()
		}

		companion object {
			const val NOT_COLLAPSED = 0
			const val SIZE = 9
			val STATES = listOf(1..9).flatten()
		}
	}
}