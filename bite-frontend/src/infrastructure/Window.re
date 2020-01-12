let redirect: string => unit = [%bs.raw
  {| function(url) { window.location.replace(url);} |}
];
