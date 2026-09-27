package com.example.hr.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hr.dto.AttachmentDto;
import com.example.hr.service.AttachmentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AttachmentController {

	private final AttachmentService service;

	// Emp 등 실제 화면에 붙이기 전, 첨부파일 기능만 독립적으로 확인하기 위한 테스트 대상
	private static final String TEST_REF_TYPE = "TEST";
	private static final String TEST_REF_ID = "1";

	/*
	 * 왜 여기서만 String/void가 아니라 ResponseEntity를 쓰는가
	 * -------------------------------------------------------
	 * testForm(), testUpload()처럼 String을 리턴하는 메서드는 "뷰 이름"을 리턴하는 것이고,
	 * 실제 HTTP 응답은 스프링이 그 이름에 해당하는 Thymeleaf 템플릿(HTML 문서)을 렌더링해서 만들어준다.
	 * 즉 지금까지의 흐름은 항상 "HTML 문서를 전달"하는 것이었다.
	 *
	 * 하지만 이 view() 메서드는 <img src="/attachments/5">, 파일 다운로드처럼
	 * HTML 문서가 아니라 이미지/파일의 바이너리(2진) 데이터 자체를 그대로 응답해야 한다.
	 * 렌더링할 뷰가 없으므로 "뷰 이름 리턴 + 템플릿 렌더링" 방식을 쓸 수가 없고,
	 * 상태코드·헤더·바디를 전부 코드에서 직접 조립해서 응답해야 한다 - 그 조립 도구가 ResponseEntity<T>다.
	 *
	 * ResponseEntity<T> : HTTP 응답 전체(상태코드 + 헤더 + 바디)를 표현하는 객체.
	 *   제네릭 타입 T = 바디에 들어갈 값의 타입. 여기서는 파일을 나타내는 Resource이므로 ResponseEntity<Resource>.
	 *
	 * - ResponseEntity.notFound().build()      -> 상태코드만 404, 바디 없음 (파일/DB 레코드가 없을 때)
	 * - ResponseEntity.ok()                    -> 상태코드 200으로 응답을 만들기 시작하는 빌더
	 *     .contentType(MediaType...)           -> Content-Type 헤더. 이게 있어야 브라우저가 "이건 image/png구나"
	 *                                              인식해서 <img> 태그 등에 제대로 그려준다 (없으면 그냥 텍스트/다운로드로 취급될 수 있음).
	 *     .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"...\"")
	 *                                           -> "다운로드 창을 띄우지 말고 화면에 바로 보여주되(inline),
	 *                                              혹시 저장할 땐 이 파일명을 써라" 를 브라우저에게 지시하는 헤더.
	 *     .body(resource)                      -> 실제 응답 바디를 채운다. 여기선 byte[]를 직접 안 읽고
	 *                                              Resource(파일을 감싼 객체)를 넘겨서, 스프링이 파일을 열어
	 *                                              응답 스트림으로 그대로 흘려보내게(스트리밍) 한다.
	 */
	// 로그인 필요 - LoginCheckInterceptor가 이미 이 경로를 보호하므로 별도 제외 처리 안 함
	@GetMapping("/attachments/{attachmentId}")
	public ResponseEntity<Resource> view(@PathVariable int attachmentId) throws IOException {
		AttachmentDto att = service.selectById(attachmentId);
		if (att == null) {
			return ResponseEntity.notFound().build();
		}

		Resource resource = new FileSystemResource(att.getStoredPath());
		if (!resource.exists() || !resource.isReadable()) {
			return ResponseEntity.notFound().build();
		}

		// body(resource)는 지금 파일을 읽는 게 아니라 "이 Resource를 응답 바디로 쓰겠다"는 계획만 세우는 것.
		// 이 메서드가 리턴한 뒤 스프링의 HttpMessageConverter가 resource.getInputStream()을 호출해
		// 그때 파일을 열고 바이트를 응답 스트림으로 흘려보낸다(스트리밍).
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(att.getContentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + att.getOriginalName() + "\"")
				.body(resource);
	}

	@GetMapping("/attachment-form")
	public String testForm(Model model) {
		model.addAttribute("attachments", service.selectByRef(TEST_REF_TYPE, TEST_REF_ID));
		return "attachment-form";
	}

	@PostMapping("/attachment-form")
	public String testUpload(@RequestParam(value = "files", required = false) List<MultipartFile> files,
			RedirectAttributes redirectAttributes) {
		try {
			if (files != null) {
				for (MultipartFile file : files) {
					if (file != null && !file.isEmpty()) {
						service.save(TEST_REF_TYPE, TEST_REF_ID, file);
					}
				}
			}
			redirectAttributes.addFlashAttribute("msg", "첨부파일이 등록되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		} catch (IOException e) {
			redirectAttributes.addFlashAttribute("error", "파일 저장 중 오류가 발생했습니다.");
		}
		return "redirect:/attachment-form";
	}
}
