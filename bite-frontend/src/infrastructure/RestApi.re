let apiUrl = "https://bitehack.codeheroes.tech";

let fetch = (path: string, params) =>
  Fetch.fetchWithInit(apiUrl ++ path, params)
  |> Repromise.Rejectable.fromJsPromise
  |> Repromise.Rejectable.andThen(res => {
       let status = Fetch.Response.status(res);
       switch (status) {
       | 200
       | 201 =>
         Fetch.Response.json(res)
         |> Repromise.Rejectable.fromJsPromise
         |> Repromise.Rejectable.map(json => Belt.Result.Ok(json))

       | status =>
         Fetch.Response.text(res)
         |> Repromise.Rejectable.fromJsPromise
         |> Repromise.Rejectable.map(text =>
              Belt.Result.Error(`Error((status, text)))
            )
       };
     })
  |> Repromise.Rejectable.catch(error =>
       Belt.Result.Error(`Exception(error)) |> Repromise.resolved
     );

module JsonData = {
  external toJson: Js.t('a) => Js.Json.t = "%identity";
  let make = body => {
    let result = body |> toJson |> Js.Json.stringify |> Fetch.BodyInit.make;
    Js.log(result);
    result;
  };
};

let defaultHeaders = token => {
  let default = {
    "Accept": "application/json",
    "Content-Type": "application/json",
  };

  let h =
    switch (token) {
    | Some(existing) =>
      let bearer = "Bearer " ++ existing;

      Js.Obj.assign(default, {"Authorization": bearer});
    | None => default
    };

  Fetch.HeadersInit.make(h);
};

let post = (~path, ~bodyJson=?, ~token=?, ~headers=?, ()) => {
  let body = Belt.Option.map(bodyJson, JsonData.make);
  let headers_ = Belt_Option.getWithDefault(headers, defaultHeaders(token));
  fetch(
    path,
    Fetch.RequestInit.make(~body?, ~method_=Post, ~headers=headers_, ()),
  );
};

let put = (~path, ~bodyJson=?, ~token=?, ~headers=?, ()) => {
  let body = Belt.Option.map(bodyJson, JsonData.make);
  let headers_ = Belt_Option.getWithDefault(headers, defaultHeaders(token));
  fetch(
    path,
    Fetch.RequestInit.make(~body?, ~method_=Put, ~headers=headers_, ()),
  );
};

let get = (~path, ~token=?, ~headers=?, ()) => {
  let headers_ = Belt_Option.getWithDefault(headers, defaultHeaders(token));
  fetch(path, Fetch.RequestInit.make(~method_=Get, ~headers=headers_, ()));
};
