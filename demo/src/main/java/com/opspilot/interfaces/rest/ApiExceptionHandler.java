package com.opspilot.interfaces.rest;

import com.opspilot.application.incident.DuplicateAlertException;
import com.opspilot.application.incident.IncidentNotFoundException;
import com.opspilot.application.mock.MockResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一 API 错误响应
 *
 * 使用 RFC 9457 {@link ProblemDetail}，避免每个 Controller 自己拼错误 JSON。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 处理资源不存在异常。
     *
     * @param exception 故障不存在或 Mock 资源不存在异常
     * @return 404 ProblemDetail
     */
    @ExceptionHandler({IncidentNotFoundException.class, MockResourceNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException exception) {
        // 把异常消息作为 detail，统一返回 404
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    /**
     * 处理重复告警异常。
     *
     * @param exception 重复告警异常
     * @return 409 ProblemDetail
     */
    @ExceptionHandler(DuplicateAlertException.class)
    public ProblemDetail handleDuplicateAlert(DuplicateAlertException exception) {
        // 同一个 alertId 重复建单属于资源冲突，返回 409
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Duplicate alert");
        return problem;
    }

    /**
     * 处理请求体字段校验失败。
     *
     * @param exception Bean Validation 校验异常
     * @return 400 ProblemDetail，errors 属性包含字段级错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        // 顶层错误只给出通用说明，具体字段错误放在 errors 中
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Validation failed");

        // 按字段聚合错误；同一字段只保留第一条消息，避免响应过于冗长
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "invalid value"
                            : fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * 处理缺少必填请求参数。
     *
     * @param exception 缺少参数异常
     * @return 400 ProblemDetail
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException exception) {
        // 把缺失的参数名写进错误详情，便于调用方修正请求
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter '" + exception.getParameterName() + "'");
        problem.setTitle("Missing request parameter");
        return problem;
    }

    /**
     * 处理请求参数类型不匹配。
     *
     * @param exception 参数类型转换异常
     * @return 400 ProblemDetail
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        // 指出哪个参数的值无法转换成目标类型
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + exception.getName() + "'");
        problem.setTitle("Invalid request parameter");
        return problem;
    }

    /**
     * 处理无法解析的请求体。
     *
     * @param exception JSON 解析异常
     * @return 400 ProblemDetail
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException exception) {
        // 不向调用方暴露底层解析细节，只提示 JSON 格式有误
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Malformed JSON request");
        problem.setTitle("Malformed request body");
        return problem;
    }

    /**
     * 处理业务代码抛出的非法参数异常。
     *
     * @param exception 非法参数异常
     * @return 400 ProblemDetail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        // 例如时间窗 from > to，直接作为 400 返回给调用方
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }
}
