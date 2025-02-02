/*
 * Copyright (c) 2022 Paolo Boni
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.paoloboni.encryption

import cats.effect.Async
import cats.implicits._
import io.github.paoloboni.binance.common.parameters.TimeParams
import io.github.paoloboni.http.QueryParamsConverter._
import io.github.paoloboni.http.UriOps
import sttp.model.Uri

class MkSignedUri[F[_]](recvWindow: Int, apiSecret: String) {

  def apply(uri: Uri, params: (String, String)*)(implicit F: Async[F]): F[Uri] =
    F.realTime.map { currentTime =>
      val timeParams   = TimeParams(recvWindow, currentTime.toMillis).toQueryParams
      val query        = timeParams.param(params.toMap)
      val uriAndParams = uri.addParams(query)
      val signature    = HMAC.sha256(apiSecret, uriAndParams.queryString)
      uriAndParams.addParam("signature", signature)
    }
}
