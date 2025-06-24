// FinancialTransactionMapper.java - CORRIGIDO
package com.example.azure_sql_demo.mapper;

import com.example.azure_sql_demo.dto.CreateTransactionRequest;
import com.example.azure_sql_demo.dto.FinancialTransactionDTO;
import com.example.azure_sql_demo.dto.TransferRequest;
import com.example.azure_sql_demo.model.FinancialTransaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FinancialTransactionMapper {

    /**
     * Convert FinancialTransaction entity to FinancialTransactionDTO
     */
    @Mapping(target = "fromAccountNumber", source = "fromAccount.accountNumber")
    @Mapping(target = "toAccountNumber", source = "toAccount.accountNumber")
    FinancialTransactionDTO toDTO(FinancialTransaction transaction);

    /**
     * Convert list of FinancialTransaction entities to DTOs
     */
    List<FinancialTransactionDTO> toDTOList(List<FinancialTransaction> transactions);

    /**
     * Convert CreateTransactionRequest to FinancialTransaction entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fromAccount", ignore = true) // Set by service
    @Mapping(target = "toAccount", ignore = true) // Set by service
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    FinancialTransaction toEntity(CreateTransactionRequest request);

    /**
     * Convert TransferRequest to FinancialTransaction entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transactionType", constant = "TRANSFER")
    @Mapping(target = "fromAccount", ignore = true) // Set by service
    @Mapping(target = "toAccount", ignore = true) // Set by service
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    FinancialTransaction toEntity(TransferRequest request);

    /**
     * Convert CreateTransactionRequest to FinancialTransactionDTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    FinancialTransactionDTO requestToDTO(CreateTransactionRequest request);
}
