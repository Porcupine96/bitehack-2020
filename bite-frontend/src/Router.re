type route =
  | Issue(string)
  | NotFound;

type route_payload = {spotify_access_token: option(string)};

let urlToRoute = (url: ReasonReact.Router.url) => {
  switch (url.path) {
  | ["issue"] => Issue(url.search)
  | ["issue", _] => Issue(url.search)
  | _ => NotFound
  };
};

let routeToUrl = route =>
  switch (route) {
  | Issue("") => "/issue"
  | Issue(query) => "/issue?" ++ query
  | _ => "/issue"
  };

let getInitialRoute = () => {
  let initRoute = ReasonReact.Router.dangerouslyGetInitialUrl();
  let route = urlToRoute(initRoute);
  ReasonReact.Router.push(routeToUrl(route));
  (route, {spotify_access_token: None});
};

let go = route => ReasonReactRouter.push(routeToUrl(route));
