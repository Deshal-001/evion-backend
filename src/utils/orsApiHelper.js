const axios = require("axios");

async function callOrs(routeRequest, orsDirectionsUrl, orsApiKey) {
  try {
    const url = `${orsDirectionsUrl}?api_key=${orsApiKey}`;
    const body = {
      coordinates: [
        [routeRequest.startLng, routeRequest.startLat],
        [routeRequest.endLng, routeRequest.endLat]
      ]
    };

    const response = await axios.post(url, body);
    return response.data;
  } catch (error) {
    throw new Error(`Failed to call ORS API: ${error.message}`);
  }
}

module.exports = {
  callOrs
};
