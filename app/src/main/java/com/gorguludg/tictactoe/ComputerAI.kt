package com.gorguludg.tictactoe

import kotlin.random.Random

class ComputerAI(private val computerSymbol: String, private val playerSymbol: String) {

    // Find the best move for the computer
    fun findBestMove(gameLogic: GameLogic): Pair<Int, Int>? {
        // 1. Try to win
        val winMove = findWinningMove(gameLogic, computerSymbol)
        if (winMove != null) return winMove

        // 2. Block player from winning
        val blockMove = findWinningMove(gameLogic, playerSymbol)
        if (blockMove != null) return blockMove

        // 3. Take center if available
        if (gameLogic.getCell(1, 1).isEmpty()) {
            return Pair(1, 1)
        }

        // 4. Take a corner
        val corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
        val availableCorners = corners.filter { gameLogic.getCell(it.first, it.second).isEmpty() }
        if (availableCorners.isNotEmpty()) {
            return availableCorners[Random.nextInt(availableCorners.size)]
        }

        // 5. Take any available cell
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (gameLogic.getCell(i, j).isEmpty()) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }

        return if (emptyCells.isNotEmpty()) {
            emptyCells[Random.nextInt(emptyCells.size)]
        } else {
            null
        }
    }

    // Find a move that would result in a win for the given player
    private fun findWinningMove(gameLogic: GameLogic, symbol: String): Pair<Int, Int>? {
        val winningCombinations = listOf(
            // Rows
            listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2)),
            listOf(Pair(1, 0), Pair(1, 1), Pair(1, 2)),
            listOf(Pair(2, 0), Pair(2, 1), Pair(2, 2)),
            // Columns
            listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0)),
            listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
            listOf(Pair(0, 2), Pair(1, 2), Pair(2, 2)),
            // Diagonals
            listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)),
            listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        )

        for (combo in winningCombinations) {
            val values = combo.map { gameLogic.getCell(it.first, it.second) }
            val symbolCount = values.count { it == symbol }
            val emptyCount = values.count { it.isEmpty() }

            // If 2 of the 3 cells have the symbol and 1 is empty, that's a winning/blocking move
            if (symbolCount == 2 && emptyCount == 1) {
                return combo.first { gameLogic.getCell(it.first, it.second).isEmpty() }
            }
        }

        return null
    }
}