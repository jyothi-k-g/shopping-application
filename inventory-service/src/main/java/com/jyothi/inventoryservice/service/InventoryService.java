package com.jyothi.inventoryservice.service;

import com.jyothi.inventoryservice.dto.InventoryResponse;
import com.jyothi.inventoryservice.repository.InventoryRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;
    @Transactional(readOnly = true)
    @SneakyThrows //don't use this in production sever
    public List<InventoryResponse> isInStock(List<String> skuCode){
        log.info("wait started");
        Thread.sleep(1000);
        log.info("wait ended");
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponse.builder()
                            .skuCode(inventory.getSkuCode())
                            .isInStock(inventory.getQuantity()>0)
                            .build()
                ).toList();
    }
}
