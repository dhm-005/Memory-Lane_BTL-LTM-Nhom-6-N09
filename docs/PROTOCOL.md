# JSON protocol contract

Client and Server are independent Maven projects. They communicate through one JSON object per TCP line. The `type` and payload field names below are a compatibility contract; do not rename them on only one side.

## Client → Server

| Type | Payload |
|---|---|
| `REGISTER` | `username`, `password` |
| `LOGIN` | `username`, `password` |
| `CHANGE_PASSWORD` | `oldPassword`, `newPassword` |
| `LOGOUT` | none |
| `LIST_ROOMS` | none |
| `CREATE_ROOM` | none |
| `JOIN_ROOM` | `roomId` |
| `READY` / `UNREADY` | none |
| `LEAVE_ROOM` | none |
| `RPS_CHOICE` | `choice`: `ROCK`, `PAPER`, `SCISSORS` |
| `CHOOSE_ORDER` | `order`: `FIRST` or `SECOND` |
| `BUY_SELECTION` | `codes[]` |
| `BUY_CONFIRM` | none |
| `FLIP` | `index` |
| `USE_CARD` | `code`, `index` (`-1` when no target is required) |
| `SKIP_CARD` | none |
| `SHIELD_RESPONSE` | `use` (boolean) |
| `LEAVE_MATCH` | none |
| `SOLO_START` | none |
| `SOLO_BEST` | none |
| `SOLO_LEVEL_COMPLETE` | `level`, `timeMs` |
| `HISTORY` | none |
| `LEADERBOARD` | none |
| `SOLO_LEADERBOARD` | none |
| `CARD_COLLECTION` | none |

## Server → Client

`ERROR(message)`, `LOGIN_OK(userId, username, coins, points)`, `PASSWORD_CHANGED`, `LOGOUT_OK`, `ROOMS(rooms[])`, `LOBBY_UPDATE`, `ROOM_JOINED(roomId, host)`, `ROOM_STATE(roomId, player1, player2, ready1, ready2)`, `ROOM_CANCELLED(reason, coins)`, `ROOM_LEFT(reason, coins)`, `RPS_START`, `RPS_WAIT`, `RPS_TIE`, `RPS_RESULT(winner, youWin)`, `ORDER_SELECTED(firstPlayer, secondPlayer)`, `CARD_OFFERS(cards[])`, `BUY_CONFIRMED(coins, owned[])`, `COINS_UPDATED(coins)`, `GAME_START(player1, player2, currentPlayer, boardSize, firstPlayer, secondPlayer, coins1, coins2)`, `PRIVATE_CARDS(cards[])`, `CARD_FLIP(index, value, by)`, `ROUND_RESULT(matched, score1, score2, indexA, indexB, player, currentPlayer, player1, player2)`, `BOARD_RESET(currentPlayer)`, `TURN_CHANGED(currentPlayer, score1, score2, lockedIndex, turnSeconds, player1, player2)`, `CARD_PROMPT(phase, eligible[])`, `CARD_USED(player, code, name)`, `CARD_EFFECT(code, target, message[, index])`, `SHIELD_PROMPT(attacker, code)`, `SHIELD_BLOCKED(player)`, `PRIVATE_REVEAL(index, value)`, `CARD_FAILED`, `BONUS_ATTEMPT`, `LUCKY_PAIR(player, indexA, indexB)`, `BOARD_SHUFFLED`, `TURN_SKIPPED(player)`, `TURN_TIMEOUT(player)`, `SOLO_LEVEL_OK(level, timeMs, coins)`, `GAME_OVER(score1, score2, winner, player1, player2, coin1, coin2, purchaseCoin1, purchaseCoin2, finishCoin1, finishCoin2, netCoin1, netCoin2, rank1, rank2, coins1, coins2)`, `MATCH_CANCELLED(reason, quitter, player1, player2, coin1, coin2, purchaseCoin1, purchaseCoin2, finishCoin1, finishCoin2, coins1, coins2)`, `HISTORY(items[])`, `LEADERBOARD(items[])`, `SOLO_LEADERBOARD(items[])`, `CARD_COLLECTION(cards[])`, `SOLO_BEST(level, timeMs)`, `SOLO_REWARD_OK(coins, amount, level)`.

## Fixed gameplay constants and timing

- PvP board: 36 cells, 18 pairs.
- Symbol catalogue: 32 entries; the `CARD_FLIP.value` is an index into the Client symbol catalogue.
- Purchase selection: up to two cards from three offered groups. A player must have at least 10 coins to create/join a 1v1 room, and purchases must leave those 10 coins reserved for the abandonment penalty.
- Server timers: 32-second purchase fallback (the Client chooser closes at 30 seconds); 60 seconds per PvP turn; 1.5 seconds before a missed pair is hidden again.
- Client-only private reveal timer: 2 seconds for `S01`.
- The Server is authoritative for PvP turns, valid card prompts, rewards, and match results. Match coin/ranking updates and the history row are committed in one database transaction.
- Solo progression is initiated by the Client, while the Server requires an authenticated session, validates sequential completion and a minimum server-measured duration per level, and records the elapsed time at the highest level reached. The client-supplied `timeMs` is ignored for records.
- Lucky Star (`S04`) has a 75% chance to automatically match a pair.
- The 1v1 leaderboard only includes users with at least one completed match. Its win rate is displayed with two decimal places.
- History, Solo leaderboard, and 1v1 leaderboard tables do not sort when column headers are clicked.

No reconnection behavior is introduced by this refactor. The Client continues to report connection loss without automatically reconnecting.


## Turn timeout

`TURN_CHANGED` includes `turnSeconds`, the remaining seconds in the current turn. Each turn lasts 60 seconds. If time expires, the Server clears any unfinished reveal or card prompt and sends `TURN_TIMEOUT` before advancing the turn. The Client countdown is display-only; the Server timer is authoritative.

## Solo confirmation

After accepting `SOLO_LEVEL_COMPLETE`, the Server sends `SOLO_LEVEL_OK` with `level`, `timeMs`, and `coins`. The Client advances to the next level or shows the final result only after this acknowledgement.
