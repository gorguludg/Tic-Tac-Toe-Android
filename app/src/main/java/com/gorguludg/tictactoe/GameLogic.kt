package com.gorguludg.tictactoe

class GameLogic {
    private val board = Array(3) { Array(3) { "" } }
    private var isXTurn = true
    private var scoreX = 0
    private var scoreO = 0
    private var gameActive = true

    init {
        initializeBoard()
    }

    // Initialize or reset the board
    fun initializeBoard() {
        for (i in 0..2) {
            for (j in 0..2) {
                board[i][j] = ""
            }
        }
        isXTurn = true
        gameActive = true
    }

    // Make a move - returns true if the move was valid
    fun makeMove(row: Int, col: Int): Boolean {
        if (!gameActive) return false
        if (board[row][col].isNotEmpty()) return false

        board[row][col] = if (isXTurn) "X" else "O"
        return true
    }

    // Switch turns after a valid move
    fun switchTurn() {
        isXTurn = !isXTurn
    }

    // Check for a winner - returns "X", "O", "Draw", or "" (game continues)
    fun checkWinner(): String {
        // Check rows
        for (i in 0..2) {
            if (board[i][0].isNotEmpty() &&
                board[i][0] == board[i][1] &&
                board[i][1] == board[i][2]
            ) {
                gameActive = false
                return board[i][0]
            }
        }

        // Check columns
        for (j in 0..2) {
            if (board[0][j].isNotEmpty() &&
                board[0][j] == board[1][j] &&
                board[1][j] == board[2][j]
            ) {
                gameActive = false
                return board[0][j]
            }
        }

        // Check diagonal (top-left to bottom-right)
        if (board[0][0].isNotEmpty() &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) {
            gameActive = false
            return board[0][0]
        }

        // Check diagonal (top-right to bottom-left)
        if (board[0][2].isNotEmpty() &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) {
            gameActive = false
            return board[0][2]
        }

        // Check for draw
        val isFull = board.all { row -> row.all { it.isNotEmpty() } }
        if (isFull) {
            gameActive = false
            return "Draw"
        }

        // Game continues
        return ""
    }

    // Returns the winning positions as a list of Pairs [row, col]
    // Returns null if no winner yet
    fun getWinningCells(): List<Pair<Int, Int>>? {
        // Check rows
        for (i in 0..2) {
            if (board[i][0].isNotEmpty() &&
                board[i][0] == board[i][1] &&
                board[i][1] == board[i][2]
            ) {
                return listOf(Pair(i, 0), Pair(i, 1), Pair(i, 2))
            }
        }

        // Check columns
        for (j in 0..2) {
            if (board[0][j].isNotEmpty() &&
                board[0][j] == board[1][j] &&
                board[1][j] == board[2][j]
            ) {
                return listOf(Pair(0, j), Pair(1, j), Pair(2, j))
            }
        }

        // Check diagonal (top-left to bottom-right)
        if (board[0][0].isNotEmpty() &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) {
            return listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
        }

        // Check diagonal (top-right to bottom-left)
        if (board[0][2].isNotEmpty() &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) {
            return listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
        }

        return null
    }

    // Update score
    fun addPointX() { scoreX++ }
    fun addPointO() { scoreO++ }

    // Getters
    fun isXTurn() = isXTurn
    fun getScoreX() = scoreX
    fun getScoreO() = scoreO
    fun isGameActive() = gameActive
    fun getCell(row: Int, col: Int) = board[row][col]
    fun getBoardCopy(): Array<Array<String>> {
        return Array(3) { i -> Array(3) { j -> board[i][j] } }
    }
    fun setBoardCell(row: Int, col: Int, value: String) {
        board[row][col] = value
    }
}