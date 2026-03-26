package com.b1nd.dodamdodam.oauth.infrastructure.exception

import com.b1nd.dodamdodam.core.common.exception.BasicException

class OauthException(val oauthCode: OauthExceptionCode) : BasicException(oauthCode)
