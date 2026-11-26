package com.kdt.fridge.user.client;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.user.client.dto.KakaoTokenResponse;
import com.kdt.fridge.user.client.dto.KakaoUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class KakaoOAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public KakaoOAuthClient(@Value("${kakao.client-id}") String clientId,
                            @Value("${kakao.client-secret}") String clientSecret,
                            @Value("${kakao.redirect-uri}") String redirectUri) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder().requestFactory(factory).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public Long getKakaoId(String authorizationCode) {
        String kakaoAccessToken = requestAccessToken(authorizationCode);
        return requestKakaoId(kakaoAccessToken);
    }

    private String requestAccessToken(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("code", authorizationCode);

        try {
            KakaoTokenResponse response = restClient.post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
                    })
                    .body(KakaoTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
            }
            return response.accessToken();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_SERVER_ERROR);
        }
    }

    private Long requestKakaoId(String kakaoAccessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri(USER_INFO_URI)
                    .header("Authorization", "Bearer " + kakaoAccessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
                    })
                    .body(KakaoUserResponse.class);

            if (response == null || response.id() == null) {
                throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
            }
            return response.id();
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KAKAO_SERVER_ERROR);
        }
    }
}