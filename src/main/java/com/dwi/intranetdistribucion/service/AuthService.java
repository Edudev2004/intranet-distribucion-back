package com.dwi.intranetdistribucion.service;

import com.dwi.intranetdistribucion.dto.request.LoginRequestDTO;
import com.dwi.intranetdistribucion.dto.request.UsuarioRequestDTO;
import com.dwi.intranetdistribucion.dto.response.JwtAuthResponseDTO;
import com.dwi.intranetdistribucion.dto.response.UsuarioResponseDTO;

public interface AuthService {

    JwtAuthResponseDTO login(LoginRequestDTO loginRequestDTO);

    UsuarioResponseDTO registrar(UsuarioRequestDTO usuarioRequestDTO);
}
