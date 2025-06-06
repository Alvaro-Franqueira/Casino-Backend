package udaw.casino.controller;

import udaw.casino.dto.BetDTO;
import udaw.casino.exception.ResourceNotFoundException;
import udaw.casino.exception.InsufficientBalanceException;
import udaw.casino.model.Bet;
import udaw.casino.service.RouletteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor; 
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller for managing roulette game operations in the casino system.
 * Provides endpoints for placing bets and playing rounds of roulette.
 */
@RestController
@RequestMapping("/api/games/roulette") 
@AllArgsConstructor 
public class RouletteController {

    private final RouletteService rouletteService;       
    private static final Logger log = LoggerFactory.getLogger(RouletteService.class);

    /**
     * Endpoint to place a bet and play a round of Roulette, generating a winning number on the server.
     * Requires authenticated user context (handled by Spring Security later).
     *
     * @param userId      The ID of the user placing the bet.
     * @param amount       The amount to bet.
     * @param betType    The type of bet (e.g., "number", "color").
     * @param betValue   The value being bet on (e.g., "17", "red").
     * @return ResponseEntity containing the winning number and the resolved BetDTO, or an error message.
     */
    @PostMapping("/play")
    public ResponseEntity<?> playRoulette(
            @RequestParam Long userId,
            @RequestParam double amount,
            @RequestParam String betType,
            @RequestParam String betValue
            ) {
                if (userId == null) {
                    log.error("User ID is required but not provided.");
                    throw new IllegalArgumentException("User ID is required but not provided.");
                }
                if (amount <= 0) {
                    log.error("Invalid bet amount: {}. Must be greater than 0.", amount);
                    throw new IllegalArgumentException("Invalid bet amount: " + amount + ". Must be greater than 0.");
                }   
                if (betType == null || betType.isEmpty()) {
                    log.error("Bet type is required but not provided.");
                    throw new IllegalArgumentException("Bet type is required but not provided.");
                }
                if (betValue == null || betValue.isEmpty()) {
                    log.error("Bet value is required but not provided.");
                    throw new IllegalArgumentException("Bet value is required but not provided.");
                }

        try {
            // Generate a random winning number (including 0 and 00)
            Random random = new Random();
            String winningNumber;
            
            // Generate a random number between 0 and 37
            // 0-36 will represent the standard numbers
            // 37 will represent "00"
            int randomNumber = random.nextInt(38);
            
            if (randomNumber == 37) {
                winningNumber = "00";
            } else {
                winningNumber = String.valueOf(randomNumber);
            }

            // DEVELOPMENT ONLY: Log the generated winning number
            // This should be removed or replaced with a proper logging mechanism in production
            System.err.println("Betting number generated: " + betValue); // Log the betting value for debugging
            System.err.println("BACK Winning number generated: " + winningNumber); // Log the winning number for debugging
            

            Bet resolvedBet = rouletteService.playRoulette(userId, amount, betType, betValue, winningNumber);

            BetDTO betDTO = new BetDTO(resolvedBet);

            // Create the response object with BetDTO to prevent circular references
            RouletteResponse response = new RouletteResponse();
            response.setResolvedBet(betDTO); // Already using BetDTO
            // Set the winning number generated by the server
            response.setWinningNumber(winningNumber);

            return ResponseEntity.ok(response); // Return 200 OK with response body

        } catch (InsufficientBalanceException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage()); // 402 Payment Required
        } catch (ResourceNotFoundException e) {
            // Could be User not found or Roulette Game not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404 Not Found
        } catch (IllegalArgumentException e) {
             // Catching potential IllegalArgumentException from service (e.g., invalid numeroGanador range, invalid bet type)
             return ResponseEntity.badRequest().body(e.getMessage()); // 400 Bad Request
        } catch (Exception e) {
             // Log the exception for internal review
             // logger.error("Unexpected error during /play request", e); // Add proper logging
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while processing the bet."); // 500 Internal Server Error
        }
    }

    /**
     * Endpoint to place multiple bets and play a round of Roulette, generating a winning number on the server.
     * Requires authenticated user context (handled by Spring Security later).
     *
     * @param requests List of bet requests.
     * @return ResponseEntity containing the winning number and the resolved BetDTOs, or an error message.
     */
    @PostMapping("/play-multibet")
    public ResponseEntity<?> playMultibet(@RequestBody List<MultibetRequest> requests) {
        try {
            // Generate a random winning number (including 0 and 00)
            Random random = new Random();
            String winningNumber;
            
            // Generate a random number between 0 and 37
            // 0-36 will represent the standard numbers
            // 37 will represent "00"
            int randomNumber = random.nextInt(38);
            
            if (randomNumber == 37) {
                winningNumber = "00";
            } else {
                winningNumber = String.valueOf(randomNumber);
            }

            // DEVELOPMENT ONLY: Log the generated winning number
            System.err.println("BACK Winning number generated: " + winningNumber); // Log the winning number for debugging
            List<RouletteResponse> responses = new ArrayList<>();

            for (MultibetRequest request : requests) {
                if (request.getUserId() == null) {
                    log.error("User ID is required but not provided.");
                    throw new IllegalArgumentException("User ID is required but not provided.");
                }
                if (request.getAmount() <= 0) {
                    log.error("Invalid bet amount: {}. Must be greater than 0.", request.getAmount());
                    throw new IllegalArgumentException("Invalid bet amount: " + request.getAmount() + ". Must be greater than 0.");
                }   
                if (request.getBetType() == null || request.getBetType().isEmpty()) {
                    log.error("Bet type is required but not provided.");
                    throw new IllegalArgumentException("Bet type is required but not provided.");
                }
                if (request.getBetValue() == null || request.getBetValue().isEmpty()) {
                    log.error("Bet value is required but not provided.");
                    throw new IllegalArgumentException("Bet value is required but not provided.");
                }
                Bet resolvedBet = rouletteService.playRoulette(
                    request.getUserId(),
                    request.getAmount(),
                    request.getBetType(),
                    request.getBetValue(),
                    winningNumber
                );

                BetDTO betDTO = new BetDTO(resolvedBet);

                RouletteResponse response = new RouletteResponse();
                response.setResolvedBet(betDTO);
                response.setWinningNumber(winningNumber);

                responses.add(response);
            }

            return ResponseEntity.ok(responses);

        } catch (InsufficientBalanceException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while processing the multibet.");
        }
    }

    @Data
    public static class MultibetRequest {
        private Long userId;
        private double amount;
        private String betType;
        private String betValue;
    }
    

    // Helper class for the response (Using Lombok @Data for brevity)
    @Data // Generates getters, setters, toString, equals, hashCode
    private static class RouletteResponse {
        private String winningNumber; // Changed from int to String to support "00"
        private BetDTO resolvedBet; // Details of the processed bet
    }

}