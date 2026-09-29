package com.anomaly.transactionanomalybackend.controller;

import com.anomaly.transactionanomalybackend.dto.CsvUploadResult;
import com.anomaly.transactionanomalybackend.model.Transaction;
import com.anomaly.transactionanomalybackend.service.AuditLogService;
import com.anomaly.transactionanomalybackend.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuditLogService auditLogService;

    public TransactionController(
            TransactionService transactionService,
            AuditLogService auditLogService) {

        this.transactionService = transactionService;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {

        return ResponseEntity.ok(
                transactionService.getAllTransactions()
        );
    }

    // =========================================================
    // GET TRANSACTION BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(
            @PathVariable Long id) {

        return transactionService
                .getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // CREATE SINGLE TRANSACTION
    // =========================================================

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody Transaction transaction,
            Authentication authentication,
            HttpServletRequest request) {

        Transaction savedTransaction =
                transactionService.createTransaction(
                        transaction
                );

        String userEmail =
                authentication != null
                        ? authentication.getName()
                        : "UNKNOWN";

        auditLogService.log(
                userEmail,
                "CREATE_TRANSACTION",
                "Created transaction TX-" +
                        savedTransaction.getId() +
                        " with amount ₹" +
                        savedTransaction.getAmount(),
                request
        );

        return ResponseEntity.ok(
                savedTransaction
        );
    }

    // =========================================================
    // CSV UPLOAD
    // =========================================================

    @PostMapping(
            value = "/upload-csv",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<CsvUploadResult> uploadCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest request) {

        CsvUploadResult result =
                transactionService.uploadCsv(file);

        String userEmail =
                authentication != null
                        ? authentication.getName()
                        : "UNKNOWN";

        auditLogService.log(
                userEmail,
                "CSV_TRANSACTION_UPLOAD",
                "CSV upload processed. Total rows: " +
                        result.getTotalRows() +
                        ", imported: " +
                        result.getImportedRows() +
                        ", failed: " +
                        result.getFailedRows(),
                request
        );

        return ResponseEntity.ok(result);
    }

    // =========================================================
    // DELETE TRANSACTION
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {

        Optional<Transaction> transaction =
                transactionService.getTransactionById(id);

        if (transaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        transactionService.deleteTransaction(id);

        String userEmail =
                authentication != null
                        ? authentication.getName()
                        : "UNKNOWN";

        auditLogService.log(
                userEmail,
                "DELETE_TRANSACTION",
                "Deleted transaction TX-" + id,
                request
        );

        return ResponseEntity.noContent().build();
    }
}