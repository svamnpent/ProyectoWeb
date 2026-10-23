package com.utp.tienda.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.tienda.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/reporte")
	public Map<String, Object> reporte() {
		return adminService.reporte();
	}
}
