package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // defines what the role of the current file
@CrossOrigin(origins = "*") //to handle data across multiple ports at a time

public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto objT) {
        User user = userRepo.findById(objT.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        double newbalance = user.getBalance() + objT.getAmount();
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction obj2 = new Transaction();
        obj2.setUserId(objT.getId());
        obj2.setAmount(objT.getAmount());
        obj2.setCurrBalance(newbalance);
        obj2.setDescription("Rs" + objT.getAmount() + " Deposit Sucessful");

        transactionRepo.save(obj2);
        return "Deposit Sucessful";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj) {
        User user = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("Not Found"));
        double newBalance = user.getBalance() - obj.getAmount();
        if (newBalance < 0) {
            return "Insufficient Balance";
        }
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction obj1 = new Transaction();

        obj1.setAmount(obj.getAmount());
        obj1.setCurrBalance(newBalance);
        obj1.setDescription("Rs" + obj.getAmount() + "Withdrawal Successful");
        obj1.setUserId(obj.getId());
        transactionRepo.save(obj1);
        return "Withdrawal Sucessful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj) {
        // fetch database for sender and reciever
        User sender = userRepo.findById(obj.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        User rec = userRepo.findByUsername(obj.getUsername());
        if (rec == null) {return "Receiver not Found";}
        if (sender.getId() == rec.getId()) {return "Self Transaction not Allowed";}
        if(obj.getAmount()<0){return "Invalid Amount";}
        double sbalance = sender.getBalance() - obj.getAmount();
        if(sbalance <0){ return "Insufficient Funds";}
        sender.setBalance(sbalance);
        double rbalance = rec.getBalance() + obj.getAmount();
        rec.setBalance(rbalance);

        userRepo.save(sender);
        userRepo.save(rec);
        // saved balance after transfer

        Transaction t1 = new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs" + obj.getAmount() + "Sent to user" +rec.getUsername() );
        t1.setUserId(sender.getId());
        transactionRepo.save(t1);

        Transaction t2 = new Transaction();
        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rbalance);
        t2.setDescription("Rs" + obj.getAmount() + "Received from user"+sender.getUsername());
        t2.setUserId(rec.getId());
        transactionRepo.save(t2);


        return "Transfer Done Sucessfull";

    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getpassbook(@PathVariable int id)
    {
        return transactionRepo.findAllByUserId(id);
    }

}