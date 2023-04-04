class WaveFunctionCollapse {
	private val grid = Grid()

	fun collapse() {
		while (!this.grid.isCollapsed()) {
			this.iterate()
		}
	}

	private fun iterate() {
		val minEntropySquare = this.getMinEntropySquare()
		this.collapseAt(minEntropySquare)
		this.propagateChangesFrom(minEntropySquare)
	}

	private fun getMinEntropySquare(): Point {
		val entropies = Array(Grid.SIZE) { y ->
			IntArray(Grid.SIZE) { x ->
				this.getPossibleStatesOf(Point(x, y)).size
			}
		}

		var minEntropySquares = mutableListOf<Point>()
		var minEntropy = Int.MAX_VALUE

		entropies.forEachIndexed { y, row ->
			row.forEachIndexed { x, entropy ->
				if (entropy == minEntropy) {
					minEntropySquares.add(Point(x, y))
				} else if (entropy < minEntropy) {
					minEntropy = entropy
					minEntropySquares = mutableListOf(Point(x, y))
				}
			}
		}

		return if (minEntropySquares.size == 1) minEntropySquares.first()
		else minEntropySquares.random()
	}

	private fun getPossibleStatesOf(point: Point): List<Int> {
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

	}

	private fun propagateChangesFrom(point: Point) {

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

		fun isCollapsed(): Boolean {
			return this.grid.none { row ->
				NOT_COLLAPSED !in row
			}
		}

		fun doesRowContain(y: Int, value: Int): Boolean {
			return value in this.grid[y]
		}

		fun doesColumnContain(z: Int, value: Int): Boolean {
			return (0 until SIZE).any { y ->
				this.get(z, y) == value
			}
		}

		fun doesBoxContain(point: Point, value: Int): Boolean {
			val boxColumn = point.x / 3 * 3
			val boxRow = point.y / 3

			return (0..2).any { y ->
				(0..2).any { x ->
					this.get(boxColumn + x, boxRow + y) == value
				}
			}
		}

		companion object {
			const val NOT_COLLAPSED = 0
			const val SIZE = 9
			val STATES = listOf(1..9).flatten()
		}
	}
}