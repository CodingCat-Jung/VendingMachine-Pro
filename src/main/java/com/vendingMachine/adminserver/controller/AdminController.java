package com.vendingMachine.adminserver.controller;

import com.vendingMachine.adminserver.entity.Sale;
import com.vendingMachine.adminserver.entity.CollectHistory;
import com.vendingMachine.adminserver.entity.Inventory;
import com.vendingMachine.adminserver.entity.Coin;
import com.vendingMachine.adminserver.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/admin")
    public String adminPage(Model model) {
        List<Sale> sales = adminService.getAllSales();
        List<CollectHistory> collects = adminService.getAllCollects();
        List<Inventory> inventoryList = adminService.getAllInventory();
        List<Coin> coinList = adminService.getAllCoins();
        int totalRevenue = adminService.calculateTotalRevenue();

        model.addAttribute("sales", sales);
        model.addAttribute("collects", collects);
        model.addAttribute("inventoryList", inventoryList);
        model.addAttribute("coinList", coinList);
        model.addAttribute("totalRevenue", totalRevenue);

        return "admin";
    }

    @PostMapping("/admin/collect")
    public String collectRevenue(@RequestParam int denomination, @RequestParam int amount, RedirectAttributes redirectAttributes) {
        String message = adminService.collectRevenue(denomination, amount);

        if (message.contains("완료")) {
            redirectAttributes.addFlashAttribute("message", message);
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", message);
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/inventory/add")
    public String addInventory(@RequestParam String name, @RequestParam int quantity, RedirectAttributes redirectAttributes) {
        String message = adminService.updateInventoryQuantity(name, quantity);

        redirectAttributes.addFlashAttribute("message", message);
        if (message.contains("성공")) {
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin";
    }


    @PostMapping("/admin/coin/add")
    public String addCoin(@RequestParam int denomination, @RequestParam int quantity, RedirectAttributes redirectAttributes) {
        String message = adminService.updateCoinQuantity(denomination, quantity);

        redirectAttributes.addFlashAttribute("message", message);
        if (message.contains("추가되었습니다")) {
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin";
    }


}
